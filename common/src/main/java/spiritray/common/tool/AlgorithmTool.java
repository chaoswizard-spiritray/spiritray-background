package spiritray.common.tool;

import com.auth0.jwt.algorithms.Algorithm;

import java.lang.reflect.InvocationTargetException;

/**
 * ClassName:AlgorithmTool
 * Package:spiritray.common.tool
 * Description:
 *
 * @Date:2022/5/28 7:30
 * @Author:灵@email
 */
public class AlgorithmTool {
    public static Algorithm getJwtAlgorithm(String methodName, String key) {
        if (methodName == null) {
            methodName = "HMAC256";
        }
        Algorithm algorithm = null;
        //通过反射创建一个算法对象
        try {
            algorithm = (Algorithm) Algorithm.class.getDeclaredMethod(methodName, String.class).invoke(null, key);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return algorithm;
    }
}
