#!/usr/bin/env groovy
import groovy.transform.EqualsAndHashCode
import groovy.transform.Sortable
import groovy.transform.ToString

@Grapes([
    @Grab(group='org.xerial', module='sqlite-jdbc', version='3.8.11.2'),
    @Grab(group='org.xerial.thirdparty', module='nestedvm', version='1.0'),
    @GrabConfig(systemClassLoader=true)
])

import java.sql.*
import org.sqlite.SQLite
import groovy.sql.Sql

/**
 * control script
 *
 * Created by fudge on 10.10.15.
 */


def cli = new CliBuilder()

cli.with {
    usage = 'mummy-ctl'
    f(longOpt: 'file', 'input file', args: 1, type: String, required: true)
}


def opt = cli.parse(args)
if (!opt) return -1

File infile = new File(opt.f)
println "using database ${infile.absolutePath}"


def sql = Sql.newInstance("jdbc:sqlite:${infile.absolutePath}", "org.sqlite.JDBC")

def qry = """
select ZHTMLTEXT from ZARTICLECONTENT, ZARTICLE
where ZARTICLECONTENT.ZARTICLE = ZARTICLE.Z_PK and
ZARTICLE.ZFLAGGED = 1
"""

sql.eachRow(qry) {
    println it
    // parse html, extract author, title, urls
}


@ToString
@EqualsAndHashCode(excludes = ['urls'])
@Sortable(excludes = ['urls'])
class Album {
    String title, artist
    List<URL> urls = []
}