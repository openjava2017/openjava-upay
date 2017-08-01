package org.openjava.upay.trade.dao;

import org.apache.ibatis.annotations.Param;
import org.openjava.upay.shared.mybatis.MybatisMapperSupport;
import org.openjava.upay.trade.model.FundTransaction;
import org.openjava.upay.trade.model.TransactionFee;
import org.openjava.upay.trade.type.TransactionStatus;
import org.openjava.upay.trade.type.TransactionType;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository("fundTransactionDao")
public interface IFundTransactionDao extends MybatisMapperSupport
{
    void createFundTransaction(FundTransaction transaction);

    void createTransactionFee(TransactionFee fee);

    FundTransaction findFundTransactionByNo(String serialNo);

    FundTransaction findFundTransactionById(Long transactionId);

    List<TransactionFee> findFeesByTransactionId(Long transactionId);

    int compareAndSetStatus(@Param("id") Long id, @Param("newStatus")TransactionStatus newStatus,
                            @Param("oldStatus") TransactionStatus oldStatus, @Param("modifiedTime") Date modifiedTime);

    int compareAndSetAmount(@Param("id") Long id, @Param("newAmount")Long newAmount,
                            @Param("oldAmount") Long oldAmount, @Param("modifiedTime") Date modifiedTime);
}