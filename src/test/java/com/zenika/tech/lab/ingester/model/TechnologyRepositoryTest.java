package com.zenika.tech.lab.ingester.model;

import static org.assertj.core.api.Assertions.*;

import org.assertj.core.api.SoftAssertions;
import org.hibernate.PersistentObjectException;
import org.junit.jupiter.api.Test;

import com.zenika.tech.lab.ingester.Constants;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityExistsException;

@QuarkusTest
class TechnologyRepositoryTest {
    @Inject
    TechnologyRepository tested;

    @Test
    @TestTransaction
    void should_find_an_existing_object() {
        //Given
        Technology source = Constants.Technologies.jest;
        tested.persist(source);
        // BEWARE! Source id is changed here by the grace of Hibernate
        assertThat(source)
        	.extracting(t -> t.id).isNotNull();

        //When
        Technology result = tested.findOrCreate(source);

        //Then
        assertThat(result)
        	.isNotNull()
        	.extracting(t -> t.id).isEqualTo(source.id);
    }

    @Test
    @TestTransaction
    void should_create_an_object() {
    	assertThatThrownBy(() -> {
	        //Given
	        Technology source = Constants.Technologies.vue;
	        source.id = 17L;
	        assertThat(source)
	        	.extracting(t -> t.id).isNotNull()
	        	.describedAs("The fact that technology id is %d will make Hibernate believe the operation to perform is an update rather than a create. This will fail on empty databases, like the one we have on GitHub CI", source.id);
	        // This persist operation will use the incorrect value of id, and thus will fail
	        tested.persist(source);

    	}).isInstanceOf(EntityExistsException.class)
    		.cause()
    			.isInstanceOf(PersistentObjectException.class)
    			.hasMessageContaining("Detached entity passed to persist");
    }
}