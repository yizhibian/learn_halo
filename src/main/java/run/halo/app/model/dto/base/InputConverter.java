package run.halo.app.model.dto.base;

import java.lang.reflect.ParameterizedType;
import java.util.Objects;
import org.springframework.lang.Nullable;
import run.halo.app.utils.BeanUtils;
import run.halo.app.utils.ReflectionUtils;

/**
 * Converter interface for input DTO.
 *
 * @author johnniang
 */
public interface InputConverter<D> {

    /**
     * Convert to domain.(shallow)
     *
     * @return new domain with same value(not null)
     */
    @SuppressWarnings("unchecked")
    default D convertTo() {
        // Get parameterized type
        ParameterizedType currentType = parameterizedType();

        // Assert not equal
        Objects.requireNonNull(currentType,
            "Cannot fetch actual type because parameterized type is null");

        /*
        * 这里获取泛型类型的第一个 因为可能有多个 这里只有一个
        *
        * 例如Map<String,Long> mapString; 这种就是多个
        * */
        Class<D> domainClass = (Class<D>) currentType.getActualTypeArguments()[0];

        /*
        * 封装BeanUtils的意义感觉不大 就是加了一些try catch这样吧 日后深究
        * */
        return BeanUtils.transformFrom(this, domainClass);
    }

    /**
     * Update a domain by dto.(shallow)
     *
     * @param domain updated domain
     */
    default void update(D domain) {
        BeanUtils.updateProperties(this, domain);
    }

    /**
     * Get parameterized type.
     *
     * @return parameterized type or null
     */

    /*
    * 反射工具类 也是自己写的
    *
    * 逻辑是需要找到这个泛型的类 因为这个this实体类可能实现了多个接口类
    * 而且其中也可能有 有泛型的接口类
    * 所以要找到对应的那个然后转换成对应的那个类
    * 例如：public class PostParam extends BasePostParam implements InputConverter<Post>
    * */
    @Nullable
    default ParameterizedType parameterizedType() {
        return ReflectionUtils.getParameterizedType(InputConverter.class, this.getClass());
    }
}

