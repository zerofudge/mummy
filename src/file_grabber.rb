class FileGrabber
    attr_reader :full_list

    def initialize(file)
        require 'cfpropertylist'
        plist = CFPropertyList::List.new(:file => file)
        CFPropertyList.native_types(plist.value).each { |item|
            @full_list = item[1] if item[0] == 'newsItems'
        }
    end

    def flagged_list
        @full_list.find_all { |item| item['flag'] }
    end
end