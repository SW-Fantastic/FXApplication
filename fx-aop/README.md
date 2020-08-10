# AOP module

## conflict with lombok
#### 与lombok的冲突
lombok在`maven`中的scope务必使用provide，并且在module-info中使用
`require static lombok;`
这样就能够避免和lombok的冲突。

基于bytebuddy，对jdk11支持相对友好。

