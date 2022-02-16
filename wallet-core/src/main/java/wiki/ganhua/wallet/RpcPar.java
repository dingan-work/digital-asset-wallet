package wiki.ganhua.wallet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author Ganhua
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RpcPar implements Serializable {
    private String jsonrpc;
    private Integer id;
    private String method;
    private List<Object> params;
}
