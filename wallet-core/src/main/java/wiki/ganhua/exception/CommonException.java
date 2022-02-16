package wiki.ganhua.exception;

import wiki.ganhua.exception.base.BaseException;

/**
 * 所有通用性的异常信息
 *
 * @author Ganhua
 * @date 2022/2/14
 */
public class CommonException extends BaseException {

    private static final long serialVersionUID = 8178522163687922126L;

    public CommonException(String module,String message){
        super(module,message);
    }

    public CommonException(String message){
        super.setMessage(message);
    }

}
