package com.example.beach_safety_water_quality.repo;

import com.example.beach_safety_water_quality.model.LedgerEntry;
import com.example.beach_safety_water_quality.model.LedgerEntryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface LedgerEntryRepo extends JpaRepository<LedgerEntry, Long> {

    @Query("SELECT SUM(l.amount) FROM LedgerEntry l WHERE l.type = :type")
    BigDecimal sumAmountByType(@Param("type") LedgerEntryType type);

    @Query("SELECT SUM(l.amount) FROM LedgerEntry l WHERE l.type = com.example.beach_safety_water_quality.model.LedgerEntryType.INCOME_PERMIT")
    BigDecimal sumTotalIncome();

    @Query("SELECT SUM(l.amount) FROM LedgerEntry l WHERE l.type = com.example.beach_safety_water_quality.model.LedgerEntryType.EXPENSE_LAB")
    BigDecimal sumTotalExpense();

    @Query("""
        SELECT SUM(l.amount)
        FROM LedgerEntry l
        WHERE l.zoneId = :zoneId
        AND l.type = :type
    """)
    BigDecimal sumAmountByZoneIdAndType(
            @Param("zoneId") Long zoneId,
            @Param("type") LedgerEntryType type
    );

    @Query("SELECT l FROM LedgerEntry l WHERE " +
           "(:zoneId IS NULL OR l.zoneId = :zoneId) AND " +
           "(:contactId IS NULL OR l.contactId = :contactId) AND " +
           "(:type IS NULL OR l.type = :type) " +
           "ORDER BY l.id DESC")
    List<LedgerEntry> findFilteredLedgerEntries(
            @Param("zoneId") Long zoneId,
            @Param("contactId") Long contactId,
            @Param("type") LedgerEntryType type
    );
}
