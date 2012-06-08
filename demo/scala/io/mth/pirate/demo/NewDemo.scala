package io.mth.pirate
package demo


object NewDemo {
  val versionmode = mode(
    flag('V', "version", "display version.")
)

  val cmd = command[DemoArgs, () => ()]("demo", List(
    versionmode
  , helpmode
  , runmode
  ))


}
