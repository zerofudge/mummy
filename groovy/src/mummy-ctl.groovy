#!/usr/bin/env groovy
@Grapes([
    @Grab(group='org.xerial', module='sqlite-jdbc', version='3.8.11.2'),
    @Grab(group='org.xerial.thirdparty', module='nestedvm', version='1.0'),
    @GrabExclude('xml-apis:xml-apis'),
    @GrabConfig(systemClassLoader=true, initContextClassLoader=true)
])

import groovy.transform.*
import groovy.sql.Sql
import static Helper.*

/**
 * control script
 *
 * Created by fudge on 10.10.15.
 */

// parse command line
def cli = new CliBuilder()
cli.with {
    usage = 'mummy-ctl'
    f(longOpt: 'file', 'input file', args: 1, type: String, required: true)
}
def opt = cli.parse(args)
if (!opt) return -1


// read database
def qry = """
select ZHTMLTEXT from ZARTICLECONTENT, ZARTICLE
where ZARTICLECONTENT.ZARTICLE = ZARTICLE.Z_PK and
ZARTICLE.ZFLAGGED = 1
"""

// extract
List<Album> albums = []
Sql.newInstance("jdbc:sqlite:${new File(opt.f as String).absolutePath}", "org.sqlite.JDBC").eachRow(qry) { r ->
    albums << new Album(
        artist: xtract(r, /Artist:/),
        title: xtract(r, /Album:/),
        style: xtract(r, /Style:/),
        urls: xtractLinks(r)
    )
}

// ...
albums.each {
    println it
}

// ----------- lib and helper -----------

class Helper {
    static String[] lines(from) { from.toString().split(/\n/) }

    static String xtract(from, regex) {
        lines(from).find{ it =~ regex }.replaceAll(/<[^>]*>/, '').replace(regex as CharSequence, '').trim()
    }

    static List<URL> xtractLinks(from) {
        boolean b = false
        List l = []
        lines(from).each { line ->
            if (line =~ /DOWNLOAD LINKS/) b = true
            if (b && line =~ /FLAC/) b = false
            if (b) {
                def m = (line =~ /.*a href="([^"]*)".*/)
                if (m) l << new URL(m.group(1))
            }
        }
        l
    }
}

@ToString
@EqualsAndHashCode(excludes = ['urls'])
@Sortable(excludes = ['urls'])
class Album {
    String title, artist, style
    List<URL> urls = []
}