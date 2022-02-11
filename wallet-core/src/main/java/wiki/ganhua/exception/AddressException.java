package wiki.ganhua.exception;

import wiki.ganhua.exception.base.BaseException;

/**
 * 地址错误
 *
 * @author Ganhua
 * @date 2022/2/9
 */
public class AddressException extends BaseException {

    public AddressException(String message){
        super("address error",message);
    }

}
