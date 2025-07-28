package ma.hibernate.dao;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import ma.hibernate.model.Phone;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class PhoneDaoImpl extends AbstractDao implements PhoneDao {
    public PhoneDaoImpl(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @Override
    public Phone create(Phone phone) {
        Transaction transaction = null;
        try (Session session = factory.openSession()) {
            transaction = session.beginTransaction();
            session.persist(phone);
            transaction.commit();
            return phone;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Can`t add phone in to DB!: " + phone, e);
        }
    }

    @Override
    public List<Phone> findAll(Map<String, String[]> params) {
        try (Session session = factory.openSession()) {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();

            CriteriaQuery<Phone> query = criteriaBuilder.createQuery(Phone.class);

            Root<Phone> phoneRoot = query.from(Phone.class);

            List<Predicate> predicates = params.entrySet().stream()
                    .filter(e -> List.of("countryManufactured", "model", "maker", "color")
                            .contains(e.getKey()))
                    .map(e -> phoneRoot.get(e.getKey()).in((Object[]) e.getValue()))
                    .collect(Collectors.toList());

            query.select(phoneRoot).where(criteriaBuilder
                    .and(predicates.toArray(new Predicate[0])));

            Optional.ofNullable(params.get("sortBy"))
                    .map(array -> array.length > 0 ? array[0] : null)
                    .filter(field -> {
                        try {
                            Phone.class.getDeclaredField(field);
                            return true;
                        } catch (NoSuchFieldException e) {
                            return false;
                        }
                    })
                    .ifPresent(sortBy -> {
                        boolean desc = Optional.ofNullable(params.get("order"))
                                .map(a -> a.length > 0 ? a[0] : null)
                                .map("desc"::equalsIgnoreCase)
                                .orElse(false);
                        query.orderBy(desc ? criteriaBuilder.desc(phoneRoot.get(sortBy))
                                : criteriaBuilder.asc(phoneRoot.get(sortBy)));
                    });
            return session.createQuery(query).getResultList();
        }
    }
}
