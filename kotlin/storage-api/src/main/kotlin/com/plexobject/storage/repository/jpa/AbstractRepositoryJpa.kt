package com.plexobject.storage.repository.jpa

import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.data.jpa.repository.support.SimpleJpaRepository
import javax.persistence.EntityManager

// See https://github.com/spring-projects/spring-data-jpa/blob/master/src/main/java/org/springframework/data/jpa/repository/support/SimpleJpaRepository.java
abstract class AbstractRepositoryJpa<T, ID>(
        entityInformation: JpaEntityInformation<T, *>, entityManager: EntityManager) :
        SimpleJpaRepository<T, ID>(entityInformation, entityManager) {

}

