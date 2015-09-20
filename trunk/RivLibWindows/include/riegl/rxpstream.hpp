// $Id: rxpstream.hpp 1118 2014-11-05 11:40:29Z RS $

#ifndef RXPSTREAM_HPP
#define RXPSTREAM_HPP

#include <riegl/config.hpp>
#include <riegl/ridataspec.hpp>
#include <riegl/rxpmarker.hpp>
#include <riegl/connection.hpp>

#include <string>
#include <stdexcept>
#include <vector>
#include <sstream>

namespace scanlib {

class rxp_packet
{
private:
    friend class rxp_istream;
    const uint32_t* begin_;
    const uint32_t* end_;
    std::vector<uint32_t> buffer;

public:
    unsigned id_main;
    unsigned id_sub;

    rxp_packet()
        : begin_(0)
        , end_(0)
        , id_main(0)
        , id_sub(0) { }

    rxp_packet(const rxp_packet& other)
        : id_main(other.id_main)
        , id_sub(other.id_sub) {
        assign(other.begin(), other.end());
    }

    template<class P>
    rxp_packet(const P& other)
        : id_main(P::id_main)
        , id_sub(P::id_sub)
    {
        typedef typename P::template rebind<uint32_t*>::type other_t;
        const std::size_t buffer_size = other_t::max_bit_width / sizeof(uint32_t) + 1;
        std::vector<uint32_t> buf(buffer_size);
        other_t ob(&(buf[0]), &(buf[0])+buffer_size, false);
        ob = other;
        assign(ob.begin(), ob.end());
    }

    template<class P>
    rxp_packet& operator=(const P& other) {
        typedef typename P::template rebind<uint32_t*>::type other_t;
        const std::size_t buffer_size = other_t::max_bit_width / sizeof(uint32_t) + 1;
        std::vector<uint32_t> buf(buffer_size);
        other_t ob(&(buf[0]), &(buf[0])+buffer_size, false);
        id_main = other.id_main;
        id_sub = other.id_sub;
        ob = other;
        assign(ob.begin(), ob.end());
        return *this;
    }

    rxp_packet& operator=(const rxp_packet& other) {
        id_main = other.id_main;
        id_sub = other.id_sub;
        assign(other.begin(), other.end());
        return *this;
    }

    const uint32_t* begin() const {
        return begin_;
    }
    const uint32_t* end() const {
        return end_;
    }

    template<class It>
    void assign(It begin, It end) {
        buffer.assign(begin, end);
        if(!buffer.empty()){
            begin_ = &(buffer[0]);
            end_ = begin_+buffer.size();
        }else{
            begin_ = end_ = 0;
        }
    }

    void move(rxp_packet& from) {
        id_main = from.id_main;
        id_sub = from.id_sub;
        if(static_cast<size_t>(from.end_ - from.begin_) == from.buffer.size() * sizeof(uint32_t)) {
            end_ = from.end_;
            begin_ = from.begin_;
            buffer.swap(from.buffer);
        } else {
            assign(from.begin(), from.end());
        }
        from.clear();
    }

    void swap(rxp_packet& other) {
        if(static_cast<size_t>(end_ - begin_) != buffer.size() * sizeof(uint32_t)) {
            assign(begin_, end_);
        }
        if(static_cast<size_t>(other.end_ - other.begin_) != other.buffer.size() * sizeof(uint32_t)) {
            other.assign(other.begin_, other.end_);
        }

        const uint32_t* temp_begin = begin_;
        const uint32_t* temp_end = end_;
        unsigned temp_main = id_main;
        unsigned temp_sub = id_sub;
        id_main = other.id_main;
        id_sub = other.id_sub;
        other.id_main = temp_main;
        other.id_sub = temp_sub;
        begin_ = other.begin_;
        end_ = other.end_;
        other.begin_ = temp_begin;
        other.end_ = temp_end;
        buffer.swap(other.buffer);
    }

    void clear() {
        id_main = id_sub = 0;
        buffer.clear();
        begin_ = end_ = 0;
    }
};

template<class P> 
P rxp_cast(const rxp_packet& x) {
    P r(typename P::template rebind<const uint32_t*>::type(x.begin(), x.end(), false));
    return r;
}

class rxp_istream
{
    struct impl;
    impl*                                   pimpl;
    bool                                    end_of_input;
    buffer                                  buf;
    lookup_table                            lookup;
    const uint32_t*                         begin;
    const uint32_t*                         end;
    package_id                              id;

    void init(const char* uri);

public:

    typedef uint64_t pos_type;
    
    rxp_istream(const char* uri);
    rxp_istream(const std::string& uri);
    ~rxp_istream();

    operator void*() {
        if (end_of_input)
            return 0;
        else
            return this;
    }

    bool good() const {
        if (end_of_input)
            return false;
        else
            return true;
    }

    rxp_istream& operator>>(package_id& x);

    template<class P>
    rxp_istream& operator>>(P& p) {
        if (!end_of_input) {
            if (P::id_main == id.main && P::id_sub == id.sub) {
                typename P::template rebind<const uint32_t*>::type r(begin, end, false); //todo: is false a good choice?
                p = r;
            }
            else {
                std::stringstream msg;
                msg << "rxp_istream: id mismatch: "
                    << "expected " << P::id_main << "." << P::id_sub << " "
                    << "got " << id.main << "." << id.sub;
                throw(std::runtime_error(msg.str()));
            }
        }
        return *this;
    }

    rxp_istream& operator>>(rxp_packet& p);
    
    pos_type tellg() const;
    rxp_istream& seekg(pos_type pos);
};

class rxp_ostream
{
    struct impl;
    impl* pimpl;

    void init(const char* uri);

public:

    rxp_ostream(const char* uri);
    rxp_ostream(const std::string& uri);
    ~rxp_ostream();

    operator void*() {
        return this; //todo: Is the stream always ok?
    }

    bool good() const {
        return true; //todo: Is the stream always ok?
    }

    rxp_ostream& operator<<(const package_id& x);
    rxp_ostream& operator<<(const rxp_packet& p);

};

} // namespace scanlib


namespace std {

template<class charT, class traits>
basic_istream<charT, traits>&
operator>>(basic_istream<charT, traits>& in, scanlib::rxp_packet& p)
{
    if (in.iword(scanlib::stream_unknown_idx)) {
        p.id_main = p.id_sub = 0;
    } else {
        p.id_main = in.iword(scanlib::stream_id_main_idx);
        p.id_sub = in.iword(scanlib::stream_id_sub_idx);
    }
    std::vector<scanlib::uint32_t> buffer;
    if (scanlib::ridataspec_read(buffer, p.id_main, p.id_sub, in)) {
        p.assign(buffer.begin(), buffer.end());
        if (in.iword(scanlib::stream_unknown_idx)) {
            p.id_main = in.iword(scanlib::stream_id_main_idx);
            p.id_sub = in.iword(scanlib::stream_id_sub_idx);
        }
    }
    else {
        p.clear();
        in.setstate(ios::failbit);
    }
    return in;
}

template<class charT, class traits>
basic_ostream<charT, traits>&
operator<<(basic_ostream<charT, traits>& out, scanlib::rxp_packet& p)
{
    scanlib::ridataspec_write(p.begin(), p.end(), p.id_main, p.id_sub, out);
    return out;
}

} // namespace std

#endif // RXPSTREAM_HPP
