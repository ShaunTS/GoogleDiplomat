package sts.libs.diplomat

import play.api.{ApplicationLoader, Configuration, Environment}
import play.api.inject.guice.GuiceApplicationLoader
import play.api.db.{
    BoneConnectionPool,
    ConnectionPool,
    Database,
    HikariCPConnectionPool,
    PooledDatabase,
    NamedDatabase
}

import javax.inject.Inject

/**
 *  @todo Extend BuiltInComponents and/or add more components
 */
trait DiplomatComponents {

    def db: Database
}

class TestComponents @Inject() (@NamedDatabase("test") val db: Database) extends DiplomatComponents


object DefaultPlayApi {

    def Env = Environment.simple()

    def Config = Configuration.load(Env)

    def HikariCP: ConnectionPool = new HikariCPConnectionPool(Env)

    def BoneCP: ConnectionPool = new BoneConnectionPool(Env)

    def Context: ApplicationLoader.Context = ApplicationLoader.createContext(Env)

    def AppLoader: ApplicationLoader = new GuiceApplicationLoader()
}
