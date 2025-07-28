package ma.hibernate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ma.hibernate.dao.PhoneDao;
import ma.hibernate.dao.PhoneDaoImpl;
import ma.hibernate.model.Phone;
import ma.hibernate.util.HibernateUtil;
import org.hibernate.SessionFactory;

public class Main {
    public static void main(String[] args) {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

        Phone iphone = new Phone();
        iphone.setModel("Iphone 16 Pro");
        iphone.setMaker("Apple Company");
        iphone.setOs("IOS");
        iphone.setColor("Black");
        iphone.setCountryManufactured("China");
        PhoneDao phoneDao = new PhoneDaoImpl(sessionFactory);
        phoneDao.create(iphone);

        Phone xiaomi = new Phone();
        xiaomi.setModel("Note 12 Pro");
        xiaomi.setMaker("Xiaomi Company");
        xiaomi.setOs("Android");
        xiaomi.setColor("White");
        xiaomi.setCountryManufactured("China");
        phoneDao.create(xiaomi);

        Phone samsung = new Phone();
        samsung.setModel("Galaxy S25 Ultra");
        samsung.setMaker("Samsung Company");
        samsung.setOs("Android");
        samsung.setColor("Grey");
        samsung.setCountryManufactured("China");
        phoneDao.create(samsung);

        Map<String, String[]> params = new HashMap<>();
        params.put("CountryManufactured", new String[]{"China"});
        params.put("Maker", new String[]{"apple", "nokia", "samsung"});
        params.put("Color", new String[]{"white", "red"});

        List<Phone> phones = phoneDao.findAll(params);
        System.out.println("Phones found by criteria: ");
        phones.forEach(System.out::println);
    }
}
