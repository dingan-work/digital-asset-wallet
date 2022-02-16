package wiki.ganhua.exception;

import wiki.ganhua.exception.base.BaseException;

/**
 * 地址错误
 *
 * @author Ganhua
 * @date 2022/2/9
 */
public class AddressException extends BaseException {

    private static final long serialVersionUID = -760873254440395496L;

    public AddressException(String message){
        super("address error",message);
    }

}
