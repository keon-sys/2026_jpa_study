package com.study.jpa.chapter05

import jakarta.persistence.Persistence

fun assignment0101() {
    val emf = Persistence.createEntityManagerFactory("chapter05")
    val em = emf.createEntityManager()
    val tx = em.transaction

    println("=== 과제 1-1: 연관관계 주인 쪽에서 FK를 설정하면 DB에 저장되는지 확인 ===")

    try {
        tx.begin()

        val teamA = Team(name = "개발팀")
        em.persist(teamA)

        val member1 = Member(name = "홍길동")
        member1.team = teamA
        em.persist(member1)

        em.flush()
        em.clear()

        val foundMember = em.find(Member::class.java, member1.id)

        println("소속 팀: ${foundMember.team?.name}")
    } catch (e: Exception) {
        if (tx.isActive) tx.rollback()
        println("예외 발생: ${e.message}")
        e.printStackTrace()
    }
    em.close()
    emf.close()
}

fun main() {
    assignment0101()
}
