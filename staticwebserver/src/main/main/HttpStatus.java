package main;

/**
 * 1. 枚举类使得编译器可以在编译期自动检查出所有可能的潜在错误；
 * 2. enum类型的每个常量在JVM中只有一个唯一实例，所以可以直接用==比较；
 * 3. 定义的enum类型总是继承自java.lang.Enum，且无法被继承；
 * 4. 只能定义出enum的实例，而无法通过new操作符创建enum的实例；
 * 5. 定义的每个实例都是引用类型的唯一实例；
 *
 * 例如，定义的枚举类Color如下：
 * public enum Color {
 *     RED, GREEN, BLUE;
 * }
 * 编译后为：
 * public final class Color extends Enum { // 继承自Enum，标记为final class
 *     // 每个实例均为全局唯一:
 *     public static final Color RED = new Color();
 *     public static final Color GREEN = new Color();
 *     public static final Color BLUE = new Color();
 *     // private构造方法，确保外部无法调用new操作符:
 *     private Color() {}
 * }
 *
 * 如果自己定义一个class且 extends Eum，会报错如下:
 * There is no default constructor available in 'java.lang.Enum'.
 */
public enum HttpStatus {
  OK(200, "成功"), NOT_FOUND(404, "没有发现资源");

  private int status_code;

  private String desc;

  HttpStatus(int status_code, String desc) {
    this.status_code = status_code;
    this.desc = desc;
  }

  public int getStatus_code() {
    return status_code;
  }

  public String getDesc() {
    return desc;
  }

}
