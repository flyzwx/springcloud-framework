package com.xianmao.common.log.utils;

import cn.hutool.core.util.URLUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.HttpUtil;
import com.xianmao.common.log.enums.LogTypeEnum;
import com.xianmao.common.log.model.SysLogInfo;
import lombok.experimental.UtilityClass;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Objects;

@UtilityClass
public class SysLogUtils {

	public SysLogInfo getSysLog() {
		HttpServletRequest request = ((ServletRequestAttributes) Objects
				.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
		SysLogInfo sysLogInfo = new SysLogInfo();
		sysLogInfo.setType(LogTypeEnum.INFO.getType());
		sysLogInfo.setRemoteAddr(ServletUtil.getClientIP(request));
		sysLogInfo.setRequestUri(URLUtil.getPath(request.getRequestURI()));
		sysLogInfo.setMethod(request.getMethod());
		sysLogInfo.setUserAgent(request.getHeader(HttpHeaders.USER_AGENT));
		sysLogInfo.setParams(HttpUtil.toParams(request.getParameterMap()));
		return sysLogInfo;
	}

	/**
	 * 获取spel 定义的参数值
	 * @param context 参数容器
	 * @param key key
	 * @param clazz 需要返回的类型
	 * @param <T> 返回泛型
	 * @return 参数值
	 */
	public <T> T getValue(EvaluationContext context, String key, Class<T> clazz) {
		SpelExpressionParser spelExpressionParser = new SpelExpressionParser();
		Expression expression = spelExpressionParser.parseExpression(key);
		return expression.getValue(context, clazz);
	}

	/**
	 * 获取参数容器
	 * @param arguments 方法的参数列表
	 * @param signatureMethod 被执行的方法体
	 * @return 装载参数的容器
	 */
	public EvaluationContext getContext(Object[] arguments, Method signatureMethod) {
		String[] parameterNames = new LocalVariableTableParameterNameDiscoverer().getParameterNames(signatureMethod);
		EvaluationContext context = new StandardEvaluationContext();
		if (parameterNames == null) {
			return context;
		}
		for (int i = 0; i < arguments.length; i++) {
			context.setVariable(parameterNames[i], arguments[i]);
		}
		return context;
	}

}
