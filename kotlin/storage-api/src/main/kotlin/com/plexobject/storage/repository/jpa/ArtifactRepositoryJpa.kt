package com.plexobject.storage.repository.jpa

import com.plexobject.storage.domain.*
import com.plexobject.storage.repository.ArtifactRepository
import org.springframework.data.jpa.repository.query.QueryUtils.toOrders
import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import javax.persistence.EntityManager
import javax.transaction.Transactional


@Transactional(Transactional.TxType.MANDATORY)
@Repository
//@Component
open class ArtifactRepositoryJpa(
        val em: EntityManager,
        val ei: JpaEntityInformation<Artifact, *> =
                JpaEntityInformationSupport.getEntityInformation(Artifact::class.java, em)) :
        ArtifactRepository,
        AbstractRepositoryJpa<Artifact, String>(ei, em) {
    //
    override fun query(pq: PaginatedQuery): PaginatedResult<Artifact> {
        val builder = em.criteriaBuilder
        val q = builder.createQuery(Artifact::class.java)
        val root = q.from(Artifact::class.java)
        q.select(root)
        val spec = ArtifactSpecification(pq)
        q.orderBy(toOrders(pq.toSort(), root, builder))
        //
        val predicate = spec.toPredicate(root, q, builder)
        if (predicate != null) {
            q.where(predicate)
        }
        //
        val resultQuery = em.createQuery(q)
        if (pq.pageNumber > 1) {
            resultQuery.setFirstResult((pq.pageNumber - 1) * pq.pageSize)
        }
        resultQuery.setMaxResults(pq.pageSize)
        val list: List<Artifact> = resultQuery.getResultList()
        val count = count(pq)
        return PaginatedResult(pq.pageNumber, pq.pageSize, count, list)
    }

    @Transactional
    override fun get(id: String) : Artifact {
        val obj = super.findById(id)
        if (obj.isPresent) {
            return obj.get()
        } else {
            throw NotFoundException("Could not find Artifact with id $id")
        }
    }


    @Transactional
    override fun delete(id: String) : Artifact {
        val obj = get(id)
        super.delete(obj)
        return obj
    }

    @Transactional
    override fun save(entity: Artifact, overwrite: Boolean): Artifact {
        entity.validate()
        try {
            val old = get(entity.id)
            if (!overwrite) {
                throw DuplicateException("Already exist artifact with id ${entity.id}")
            }
            old.update(entity)
            return super.save(old)
        } catch (e: NotFoundException) {
            return super.save(entity)
        }
    }

    override fun saveProperties(id: String, labels: String, params: Map<String, String>): Artifact {
        val old = get(id)
        old.mergeLabels(Artifact.stringToSet(labels))
        old.addProperties(params)
        return super.save(old)
    }

    private fun count(pq: PaginatedQuery): Long {
        val builder = em.criteriaBuilder
        val q = builder.createQuery(Long::class.java)
        val root = q.from(Artifact::class.java)
        q.select(builder.count(root))
        val spec = ArtifactSpecification(pq)
        val predicate = spec.toPredicate(root, q, builder)
        if (predicate != null) {
            q.where(predicate)
        }
        return em.createQuery(q).getSingleResult()
    }
}

