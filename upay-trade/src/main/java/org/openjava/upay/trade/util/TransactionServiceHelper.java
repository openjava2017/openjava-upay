package org.openjava.upay.trade.util;

import org.openjava.upay.core.domain.FundActivity;
import org.openjava.upay.core.type.Action;
import org.openjava.upay.core.type.Pipeline;
import org.openjava.upay.core.type.StatementType;
import org.openjava.upay.trade.domain.Fee;
import org.openjava.upay.trade.model.TransactionFee;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class TransactionServiceHelper
{
    public static List<TransactionFee> wrapTransactionFees(Long transactionId, List<Fee> fees, Date when)
    {
        List<TransactionFee> feeList = new ArrayList<>();
        for (Fee fee : fees) {
            TransactionFee transactionFee = new TransactionFee();
            transactionFee.setTransactionId(transactionId);
            transactionFee.setPipeline(fee.getPipeline());
            transactionFee.setType(fee.getType());
            transactionFee.setAmount(fee.getAmount());
            transactionFee.setCreatedTime(when);
            feeList.add(transactionFee);
        }
        return feeList;
    }

    public static void wrapFeeActivitiesForAccount(List<FundActivity> activities, List<TransactionFee> fees)
    {
        for (TransactionFee fee : fees) {
            if (fee.getPipeline() == Pipeline.ACCOUNT) { //费用支出通过账户扣减方式
                FundActivity feeActivity = new FundActivity();
                feeActivity.setTransactionId(fee.getTransactionId());
                feeActivity.setPipeline(fee.getPipeline());
                feeActivity.setAction(Action.OUTGO);
                feeActivity.setType(StatementType.getType(fee.getType().getCode()));
                feeActivity.setAmount(fee.getAmount());
                feeActivity.setDescription(fee.getType().getName() + Action.OUTGO.getName());
                activities.add(feeActivity);
            }
        }
    }

    public static List<FundActivity> wrapFeeActivitiesForMer(List<TransactionFee> fees)
    {
        List<FundActivity> activities = new ArrayList<>();
        for (TransactionFee fee : fees) {
            FundActivity feeActivity = new FundActivity();
            feeActivity.setTransactionId(fee.getTransactionId());
            feeActivity.setPipeline(fee.getPipeline());
            feeActivity.setAction(Action.INCOME);
            feeActivity.setType(StatementType.getType(fee.getType().getCode()));
            feeActivity.setAmount(fee.getAmount());
            feeActivity.setDescription(fee.getType().getName() + Action.INCOME.getName());
            activities.add(feeActivity);
        }
        return activities;
    }
}
