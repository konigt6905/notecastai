package com.notecastai.common.query;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CriteriaQueryBuilder<T extends Serializable> {

    private final Class<T> entityClass;
    private final EntityManager em;

    private final List<Function<Ctx<T>, Predicate>> predicateSuppliers = new ArrayList<>();
    private boolean distinct = false;

    public static <T extends Serializable> CriteriaQueryBuilder<T> forEntity(Class<T> entityClass,
                                                                             EntityManager em) {
        return new CriteriaQueryBuilder<>(entityClass, em);
    }

    public CriteriaQueryBuilder<T> where(Function<PredicateBuilder<T>, PredicateBuilder<T>> fn) {
        PredicateBuilder<T> pb = new PredicateBuilder<>();
        PredicateBuilder<T> built = fn.apply(pb);
        predicateSuppliers.addAll(built.predicates);
        return this;
    }

    public CriteriaQueryBuilder<T> distinct() {
        this.distinct = true;
        return this;
    }

    /** Execute and return a Page. */
    public Page<T> paginate(Pageable pageable) {
        Ctx<T> ctx = new Ctx<>(em, entityClass);
        CriteriaQuery<T> cq = ctx.cb.createQuery(entityClass);
        Root<T> root = cq.from(entityClass);

        List<Predicate> preds = buildPredicates(ctx.withRoot(root));
        if (!preds.isEmpty()) cq.where(preds.toArray(Predicate[]::new));
        cq.select(root);
        cq.distinct(distinct);
        applySort(ctx, root, cq, pageable.getSort());

        TypedQuery<T> dataQuery = em.createQuery(cq)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize());

        List<T> content = dataQuery.getResultList();
        long total = count(ctx, preds);

        return new PageImpl<>(content, pageable, total);
    }

    // ---------- internals ----------

    private List<Predicate> buildPredicates(Ctx<T> ctx) {
        List<Predicate> out = new ArrayList<>();
        for (Function<Ctx<T>, Predicate> s : predicateSuppliers) {
            Predicate p = s.apply(ctx);
            if (p != null) out.add(p);
        }
        return out;
    }

    private long count(Ctx<T> ctx, List<Predicate> preds) {
        CriteriaQuery<Long> cq = ctx.cb.createQuery(Long.class);
        Root<T> root = cq.from(entityClass);
        cq.select(ctx.cb.countDistinct(root));
        if (!preds.isEmpty()) cq.where(preds.stream()
                .map(p -> p) // rebuild against this root:
                .toArray(Predicate[]::new));
        // We must rebuild predicates for the new root (different instance)
        // So we reconstruct them via suppliers again:
        List<Predicate> rebuilt = new ArrayList<>();
        for (Function<Ctx<T>, Predicate> s : predicateSuppliers) {
            Predicate p = s.apply(ctx.withRoot(root));
            if (p != null) rebuilt.add(p);
        }
        if (!rebuilt.isEmpty()) cq.where(rebuilt.toArray(Predicate[]::new));
        return em.createQuery(cq).getSingleResult();
    }

    private void applySort(Ctx<T> ctx, Root<T> root, CriteriaQuery<T> cq, Sort sort) {
        if (sort == null || sort.isUnsorted()) return;
        List<Order> orders = new ArrayList<>();
        for (Sort.Order o : sort) {
            Path<?> path = resolvePath(root, o.getProperty());
            orders.add(o.isAscending() ? ctx.cb.asc(path) : ctx.cb.desc(path));
        }
        cq.orderBy(orders);
    }

    /** Resolve simple dot-paths (e.g., "user.id"). */
    private Path<?> resolvePath(From<?, ?> from, String dotPath) {
        String[] parts = dotPath.split("\\.");
        Path<?> path = from;
        for (String p : parts) {
            path = path.get(p);
        }
        return path;
    }

    // ---------- helper types ----------

    private record Ctx<T>(EntityManager em, CriteriaBuilder cb, Class<T> entityClass, Root<T> root) {
        Ctx(EntityManager em, Class<T> entityClass) {
            this(em, em.getCriteriaBuilder(), entityClass, null);
        }
        Ctx<T> withRoot(Root<T> root) {
            return new Ctx<>(em, cb, entityClass, root);
        }
    }

    public static class PredicateBuilder<T> {
        private final List<Function<Ctx<T>, Predicate>> predicates = new ArrayList<>();

        /** Equals on dot-path. Skips when value is null. */
        public PredicateBuilder<T> equal(String dotPath, Object value) {
            if (value == null) return this;
            predicates.add(ctx -> ctx.cb.equal(resolve(ctx.root, dotPath), value));
            return this;
        }

        /** Join collection by name and compare column on joined entity (LEFT). Skips when value is null. */
        public PredicateBuilder<T> joinEqual(String collection, String attribute, Object value) {
            if (value == null) return this;
            predicates.add(ctx -> {
                Join<?, ?> join = ctx.root.join(collection, JoinType.LEFT);
                return ctx.cb.equal(join.get(attribute), value);
            });
            return this;
        }

        /** Join collection and check if attribute is IN a list of values. Skips when collection is null or empty. */
        public PredicateBuilder<T> joinIn(String collection, String attribute, Collection<?> values) {
            if (values == null || values.isEmpty()) return this;
            predicates.add(ctx -> {
                Join<?, ?> join = ctx.root.join(collection, JoinType.LEFT);
                return join.get(attribute).in(values);
            });
            return this;
        }

        /** >= on Comparable dot-path. Skips when value is null. */
        @SuppressWarnings({"rawtypes", "unchecked"})
        public <X extends Comparable> PredicateBuilder<T> greaterThanOrEqual(String dotPath, X value) {
            if (value == null) return this;
            predicates.add(ctx -> ctx.cb.greaterThanOrEqualTo((Path<Comparable>) resolve(ctx.root, dotPath), value));
            return this;
        }

        /** < on Comparable dot-path. Skips when value is null. */
        @SuppressWarnings({"rawtypes", "unchecked"})
        public <X extends Comparable> PredicateBuilder<T> lessThan(String dotPath, X value) {
            if (value == null) return this;
            predicates.add(ctx -> ctx.cb.lessThan((Path<Comparable>) resolve(ctx.root, dotPath), value));
            return this;
        }

        private Path<?> resolve(Root<T> root, String dotPath) {
            String[] parts = dotPath.split("\\.");
            Path<?> path = root;
            for (String p : parts) {
                path = path.get(p);
            }
            return path;
        }
    }
}