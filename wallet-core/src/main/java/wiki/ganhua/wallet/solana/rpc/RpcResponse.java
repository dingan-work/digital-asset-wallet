package wiki.ganhua.wallet.solana.rpc;

import lombok.Getter;
import lombok.Setter;

/**
 * Rpc 返回
 * @author Ganhua
 */
@Getter
@Setter
public class RpcResponse<T> {

    @Getter
    @Setter
    public static class Error {
        private long code;
        private String message;
    }

    private String jsonrpc;
    private T result;
    private Error error;
    private String id;
}
