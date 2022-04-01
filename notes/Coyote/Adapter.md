## Overview
Adapter 是 coyote servlet 容器实现的主要接口。

## Implementation
[CoyoteAdapter](./Adapter/CoyoteAdapter.md) 作为 Adapter 接口的实现，实现了一个请求处理器，该处理器将处理
委托给 Coyote 处理器。