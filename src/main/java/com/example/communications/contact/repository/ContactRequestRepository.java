package com.example.communications.contact.repository;

import com.example.communications.contact.model.ContactRequest;
import com.example.communications.contact.model.ContactRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ContactRequestRepository extends JpaRepository<ContactRequest, Long> {

    List<ContactRequest> findByReceiverIdAndStatusOrderByCreatedAtDesc(
            Long receiverId,
            ContactRequestStatus status
    );

    Optional<ContactRequest> findByIdAndReceiverIdAndStatus(
            Long id,
            Long receiverId,
            ContactRequestStatus status
    );

    @Query("""
            select count(contactRequest) > 0
            from ContactRequest contactRequest
            where (
                contactRequest.sender.id = :firstUserId
                and contactRequest.receiver.id = :secondUserId
            ) or (
                contactRequest.sender.id = :secondUserId
                and contactRequest.receiver.id = :firstUserId
            )
            """)
    boolean existsBetweenUsers(
            @Param("firstUserId") Long firstUserId,
            @Param("secondUserId") Long secondUserId
    );

    @Query("""
            select count(contactRequest) > 0
            from ContactRequest contactRequest
            where contactRequest.status = com.example.communications.contact.model.ContactRequestStatus.ACCEPTED
            and (
                (
                    contactRequest.sender.id = :firstUserId
                    and contactRequest.receiver.id = :secondUserId
                ) or (
                    contactRequest.sender.id = :secondUserId
                    and contactRequest.receiver.id = :firstUserId
                )
            )
            """)
    boolean areAcceptedContacts(
            @Param("firstUserId") Long firstUserId,
            @Param("secondUserId") Long secondUserId
    );

    @Query("""
            select contactRequest
            from ContactRequest contactRequest
            join fetch contactRequest.sender
            join fetch contactRequest.receiver
            where contactRequest.status = com.example.communications.contact.model.ContactRequestStatus.ACCEPTED
            and (
                contactRequest.sender.id = :userId
                or contactRequest.receiver.id = :userId
            )
            order by contactRequest.createdAt desc
            """)
    List<ContactRequest> findAcceptedContactsForUser(@Param("userId") Long userId);
}