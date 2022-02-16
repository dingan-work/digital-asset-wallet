package wiki.ganhua.exception;

import wiki.ganhua.exception.base.BaseException;

import java.math.BigInteger;

/**
 * 交易异常
 *
 * @author Ganhua
 * @date 2022/2/14
 */
public class TransactionException extends BaseException {

    private static final long serialVersionUID = 6339502566167955030L;

    public TransactionException(String message){
        super("transaction failed",message);
    }

}
