package sts.test

package object helpers {

    import java.util.Date
    import java.text.SimpleDateFormat
    import org.specs2.matcher.MustThrownMatchers

    class VerifiableDate(val testDate: Date) extends MustThrownMatchers {

        def hasTimestamp(stamp: String) = {
            val fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSX")
            val parsed = fmt.parse(stamp)

            this.testDate.getTime must equalTo(parsed.getTime)
        }
    }

    object VerifyDate {

        def apply(testDate: Date) = new VerifiableDate(testDate)
    }
}