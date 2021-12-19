package com.agora.data;

import com.agora.data.provider.IMessageSource;
import com.agora.data.provider.IStoreSource;

/**
 * 数据仓库接口
 *
 * @author chenhengfei(Aslanchen)
 */
@Deprecated
public interface IDataRepositroy extends IStoreSource, IMessageSource {
}
