package wiki.ganhua.exception;

import wiki.ganhua.exception.base.BaseException;

/**
 * rpc请求 异常信息
 *
 * @author Ganhua
 * @date 2022/2/15
 */
public class RpcException extends BaseException {

    private static final long serialVersionUID = -6839277730972094286L;

    private String title;

    public RpcException(String title,String message){
        super("Rpc request exception",message);
        this.title = title;
    }
}
