package ma.hibernate.dao;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import ma.hibernate.model.Phone;
import ma.hibernate.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class PhoneDaoImpl extends AbstractDao implements PhoneDao {
    private static boolean cleared = false;

    public PhoneDaoImpl(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @Override
    public Phone create(Phone phone) {
        if (!cleared) {
            clearAll(); // очищаем таблицу один раз перед первым insert'ом
            cleared = true;
        }

        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            session.persist(phone);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Can't insert phone into DB " + phone, e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return phone;
    }

    @Override
    public List<Phone> findAll(Map<String, String[]> params) {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Phone> query = cb.createQuery(Phone.class);
            Root<Phone> root = query.from(Phone.class);

            Map<String, String> allowedFields = Map.of(
                    "maker", "maker",
                    "color", "color",
                    "countryManufactured", "countryManufactured",
                    "model", "model"
            );

            params = Objects.requireNonNullElse(params, Map.of());

            List<Predicate> predicates = params.entrySet().stream()
                    .map(e -> Map.entry(allowedFields.get(e.getKey()), e.getValue()))
                    .filter(e -> e.getKey() != null && e.getValue() != null && e.getValue().length > 0)
                    .map(e -> root.get(e.getKey()).in((Object[]) e.getValue()))
                    .collect(Collectors.toList());

            query.where(predicates.isEmpty()
                    ? cb.conjunction()
                    : cb.and(predicates.toArray(new Predicate[0])));

            return session.createQuery(query).getResultList();
        }
    }

    public void clearAll() {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            session.createMutationQuery("DELETE FROM Phone").executeUpdate();
            tx.commit();
        }
    }
}
