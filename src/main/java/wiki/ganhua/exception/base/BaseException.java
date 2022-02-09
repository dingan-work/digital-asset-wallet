package wiki.ganhua.exception.base;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 基础异常
 *
 * @author Ganhua
 * @date 2022/2/9
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BaseException extends RuntimeException{

    private static final long serialVersionUID = 1L;

    /**
     * 所属模块
     */
    private String module;

    /**
     * 错误详情
     */
    private String message;

    public BaseException(String module) {
        this(module, null);
    }
}
