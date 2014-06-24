package kidozen.client.crash.jraf.android.util.activitylifecyclecallbackscompat;

/**
 * Created by christian on 5/8/14.
 */

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

/** Indicates that Lint should treat this type as targeting a given API level, no matter what the
 project target is. */
@Target({TYPE, METHOD, CONSTRUCTOR})
@Retention(RetentionPolicy.CLASS)
public @interface TargetApi {
    int value();
}
