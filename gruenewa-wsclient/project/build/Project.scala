import sbt._

import de.element34.sbteclipsify._


class Project(info: ProjectInfo) extends DefaultProject(info) with Eclipsify
{

  lazy val hi = task { println("Hello World"); None }

  override def ivyXML =
    <dependencies>
      <dependency org="com.sun.xml.wss" name="xws-security" rev="3.0">
        <exclude module="xmldsig"/>
      </dependency>
    </dependencies>

  val javaNetRepo = JavaNet1Repository

  override def compileOptions = super.compileOptions ++ compileOptions("-encoding", "UTF8")

  override def javaCompileOptions = JavaCompileOption("-Xmx1024m") :: JavaCompileOption("-Xss256m") :: Nil
}
