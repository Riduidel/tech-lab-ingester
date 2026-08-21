package com.zenika.tech.lab.ingester.model;

import com.zenika.tech.lab.ingester.model.Technology;
import com.zenika.tech.lab.ingester.model.TechnologyRepository;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class TechnologyRepositoryTest {
    @Inject
    TechnologyRepository returned;

    @Test
    @TestTransaction
    void should_find_an_existing_object() {
        //Given
        Technology source = new Technology();
        source.packageManagerUrl = "https://www.npmjs.com/package/jest";
        returned.persist(source);

        //When
        Technology result = returned.findOrCreate(source);

        //Then
        assertNotNull(result);
        assertEquals(source.id,result.id);
    }

    @Test
    @TestTransaction
    void should_create_an_object() {
        //Given
        Technology source = new Technology();
        source.packageManagerUrl = "https://www.npmjs.com/package/vue";
        returned.persist(source);

        //When
        Technology result = returned.findOrCreate(source);

        //Then
        assertNotNull(result);
        assertNotNull(result.id);

        assertEquals( source.packageManagerUrl, result.packageManagerUrl);

    }
}