package com.smartinventorysystem.config.db;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJpaRepositories(
        basePackages = {
                "com.smartinventorysystem.modules.product.repository",
                "com.smartinventorysystem.modules.productimage.repository",
                "com.smartinventorysystem.modules.productsupplier.repository",
                "com.smartinventorysystem.modules.category.repository",
                "com.smartinventorysystem.modules.unit.repository",
                "com.smartinventorysystem.modules.supplier.repository",
                "com.smartinventorysystem.modules.customer.repository",
                "com.smartinventorysystem.modules.purchase.repository",
                "com.smartinventorysystem.modules.sale.repository",
                "com.smartinventorysystem.modules.stockmovement.repository",
                "com.smartinventorysystem.modules.notification.repository"
        },
        entityManagerFactoryRef = "simsEntityManagerFactory",
        transactionManagerRef = "simsTransactionManager"
)
public class SIMSDatabaseConfig {

    @Primary
    @Bean(name = "simsDataSource")
    @ConfigurationProperties(prefix = "sims.datasource")
    public DataSource simsDataSource() {

        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = "simsEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean simsEntityManagerFactory(
            @Qualifier("simsDataSource") DataSource dataSource) {

        LocalContainerEntityManagerFactoryBean factory =
                new LocalContainerEntityManagerFactoryBean();

        factory.setDataSource(dataSource);

        factory.setPackagesToScan(
                "com.smartinventorysystem.modules.product.entity",
                "com.smartinventorysystem.modules.productimage.entity",
                "com.smartinventorysystem.modules.category.entity",
                "com.smartinventorysystem.modules.unit.entity",
                "com.smartinventorysystem.modules.supplier.entity",
                "com.smartinventorysystem.modules.customer.entity",
                "com.smartinventorysystem.modules.purchase.entity",
                "com.smartinventorysystem.modules.sale.entity",
                "com.smartinventorysystem.modules.productsupplier.entity",
                "com.smartinventorysystem.modules.stockmovement.entity",
                "com.smartinventorysystem.modules.notification.entity"
        );

        factory.setPersistenceUnitName("sims");

        HibernateJpaVendorAdapter adapter =
                new HibernateJpaVendorAdapter();

        factory.setJpaVendorAdapter(adapter);

        Map<String,Object> properties = new HashMap<>();

        properties.put(
                "hibernate.hbm2ddl.auto",
                "validate"
        );

        properties.put(
                "hibernate.show_sql",
                true
        );

        properties.put(
                "hibernate.dialect",
                "org.hibernate.dialect.MySQLDialect"
        );

        factory.setJpaPropertyMap(properties);

        return factory;
    }

    @Primary
    @Bean(name = "simsTransactionManager")
    public PlatformTransactionManager inventoryTransactionManager(
            @Qualifier("simsEntityManagerFactory")
            EntityManagerFactory entityManagerFactory) {

        return new JpaTransactionManager(entityManagerFactory);
    }

}