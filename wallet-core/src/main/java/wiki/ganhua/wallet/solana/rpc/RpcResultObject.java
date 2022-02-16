package wiki.ganhua.wallet.solana.rpc;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * solana rpc 接口通用返回
 * @author Ganhua
 */
public class RpcResultObject {
    public static class Context {
        @JSONField(name = "slot")
        private long slot;

        public long getSlot() {
            return slot;
        }

    }

    @JSONField(name = "context")
    protected Context context;

    public Context gContext() {
        return context;
    }

}
