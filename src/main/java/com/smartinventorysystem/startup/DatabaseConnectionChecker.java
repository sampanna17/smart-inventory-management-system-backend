package com.smartinventorysystem.startup;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.sql.DataSource;
import java.sql.Connection;

@Component
public class DatabaseConnectionChecker implements CommandLineRunner {

    private final DataSource authDataSource;
    private final DataSource simsDataSource;

    public DatabaseConnectionChecker(
            @Qualifier("authDataSource")
            DataSource authDataSource,

            @Qualifier("simsDataSource")
            DataSource simsDataSource
    ) {
        this.authDataSource = authDataSource;
        this.simsDataSource = simsDataSource;
    }

    @Override
    public void run(String @NonNull ... args) {

        checkConnection("AUTH DB", authDataSource);
        checkConnection("SIMS DB", simsDataSource);

    }

    private void checkConnection(
            String name,
            DataSource dataSource
    ){
        try(Connection connection = dataSource.getConnection()){

            System.out.println(
                    name + " Connected Successfully"
            );

        }catch(Exception e){

            System.out.println(
                    name + " Connection Failed"
            );

        }
    }
}