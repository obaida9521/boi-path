package com.developerobaida.boipath.data.repository

import com.developerobaida.boipath.data.local.dao.AnnotationDao
import com.developerobaida.boipath.data.local.entity.AnnotationEntity

class AnnotationRepository(private val dao: AnnotationDao) {

    suspend fun insert(annotation: AnnotationEntity) = dao.insertAnnotation(annotation)

    suspend fun getAnnotations(bookId: Int): List<AnnotationEntity> = dao.getAnnotationsForBook(bookId)

    suspend fun delete(id: Int) = dao.deleteAnnotation(id)

    suspend fun clearForBook(bookId: Int) = dao.deleteAllAnnotationsForBook(bookId)
}