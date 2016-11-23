// $Id: rxpstream.hpp 686 2012-06-18 09:46:12Z pa $

#ifndef RXPSTREAM_HPP
#define RXPSTREAM_HPP

#include <riegl/config.hpp>
#include <riegl/ridataspec.hpp>
#include <riegl/rxpmarker.hpp>
#include <riegl/connection.hpp>

#ifndef RIVLIB_TR1_DIR
#   if defined(_MSC_VER)
#       include <memory>
#   else
#       include <tr1/memory>
#   endif
#else
#   include <tr1/memory>
#endif

#include <string>
#include <stdexcept>
#include <vector>
#include <sstream>

namespace scanlib {

class rxp_packet
{
    friend class rxp_istream;
    const uint32_t* begin_;
    const uint32_t* end_;
    std::vector<uint32_t> buffer;

public:
    rxp_packet();
    rxp_packet(const rxp_packet& other);

    template<class P>
    rxp_packet(const P& other) {
        typedef typename P::template rebind<uint32_t*>::type other_t;
        const std::size_t buffer_size = other_t::max_bit_width / sizeof(uint32_t) + 1;
        uint32_t buffer[buffer_size];
        other_t ob(buffer, buffer+buffer_size, false);
        ob = other;
        id_main = other_t::id_main;
        id_sub = other_t::id_sub;
        assign(ob.begin(), ob.end());
    }

    rxp_packet& operator=(const rxp_packet& other);

    unsigned id_main;
    unsigned id_sub;

    const uint32_t* begin() const {
        return begin_;
    }
    const uint32_t* end() const {
        return end_;
    }

    template<class It>
    void assign(It begin, It end) {
        buffer.clear();
        for (It it=begin; it!=end; ++it)
            buffer.push_back(*it);
        begin_ = &(buffer[0]);
        end_ = begin_+buffer.size();
    }

    void clear() {
        id_main = id_sub = 0;
        buffer.clear();
        begin_ = end_ = 0;
    }
};

template<class P> 
P rxp_cast(rxp_packet& x) {
    P r(typename P::template rebind<const uint32_t*>::type(x.begin(), x.end(), false));
    return r;
}

class rxp_istream
{
    bool                                    end_of_input;
    std::tr1::shared_ptr<basic_rconnection> rc;
    std::tr1::shared_ptr<decoder_rxpmarker> dec;
    buffer                                  buf;
    lookup_table                            lookup;
    const uint32_t*                         begin;
    const uint32_t*                          end;
    package_id                              id;

    void init(const char* uri);

public:

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
};

class rxp_ostream
{
    std::tr1::shared_ptr<basic_wconnection> wc;
    std::tr1::shared_ptr<encoder_rxpmarker> enc;
    buffer                                  buf;
    lookup_table                            lookup;
    package_id                              id;

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

    rxp_ostream& operator<<(const package_id& x) {
        id = x;
        return *this;
    }

    template<class P>
    rxp_ostream& operator<<(const P& src) {
        if (P::id_main == id.main && P::id_sub == id.sub) {
            buffer_package<typename P::template rebind<uint32_t*>::type > dst(
                lookup, buf
            );
            dst = src;
            dst.resize();
            enc->put(buf);
        }
        else
            throw(std::runtime_error("rxp_ostream: id mismatch"));
        return *this;
    }

    template<class other_it>
    rxp_ostream& operator<<(const header<other_it>& src) {
        if (1 == id.main && 0 == id.sub) {
            buffer_package<header<uint32_t*> > dst(buf);
            dst = src;
            lookup_table lu;
            lu.load(dst.id_lookup);
            lookup = lu;
            dst.resize();
            enc->put(buf);
        }
        else
            throw(std::runtime_error("rxp_ostream: id mismatch"));
        return *this;
    }

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
