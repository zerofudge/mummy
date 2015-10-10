#!/usr/bin/env groovy
@Grapes([
    @Grab(group='org.xerial', module='sqlite-jdbc', version='3.8.11.2'),
    @Grab(group='org.xerial.thirdparty', module='nestedvm', version='1.0'),
    @GrabExclude('xml-apis:xml-apis'),
    @GrabConfig(systemClassLoader=true, initContextClassLoader=true)
])

import groovy.transform.*
import groovy.sql.Sql

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

List<Album> albums = []
Sql.newInstance("jdbc:sqlite:${new File(opt.f).absolutePath}", "org.sqlite.JDBC").eachRow(qry) { r ->
    // TODO urls
    albums << new Album(
        artist: xtract(r, /Artist:/),
        title: xtract(r, /Album:/),
        style: xtract(r, /Style:/)
    )
    println r
    // (DONWLOAD LINKS .. FLAC) each s/href="([^"]*)"/\1/
}

// ...

// ----------- lib and helper -----------

String xtract(from, regex) {
    from.toString().split(/\n/).find{ it =~ regex }.replaceAll(/<[^>]*>/, '').replace(regex, '').trim()
}

@ToString
@EqualsAndHashCode(excludes = ['urls'])
@Sortable(excludes = ['urls'])
class Album {
    String title, artist, style
    List<URL> urls = []
}