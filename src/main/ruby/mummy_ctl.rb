require 'optparse'
require './file_grabber'

INFILE = '/Users/fudge/Library/Application Support/NetNewsWire/Feeds.noindex/newalbumreleases_net_?feed=rss2'

OptionParser.new(ARGV) { |p|
    p.on('-f FOO', '--foo') { |f|
        puts "may the #{f} be with ya"
    }
}

grabber = FileGrabber.new(INFILE)

puts "i got #{grabber.full_list.size} entries, #{grabber.flagged_list.size} flagged"

