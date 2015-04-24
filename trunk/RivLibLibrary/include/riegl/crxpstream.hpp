// $Id: crxpstream.hpp 692 2012-06-20 08:42:58Z pa $

#ifndef CRXPSTREAM_HPP
#define CRXPSTREAM_HPP

#include <riegl/rxpstream.hpp>

#include <string>

namespace scanlib {

class crxp_istream
{
    struct impl;
    impl* pimpl;

    bool end_of_file;
    rxp_istream is;
    rxp_packet pkg;

public:

    crxp_istream(const char* uri);
    crxp_istream(const std::string& uri);
    ~crxp_istream();

    operator void*();
    bool good() const;

    crxp_istream& operator>>(package_id& x);

    template<class P>
    crxp_istream& operator>>(P& p) {
        if (P::id_main == pkg.id_main && P::id_sub == pkg.id_sub) {
            typename P::template rebind<const uint32_t*>::type r(pkg.begin(), pkg.end(), false);
            p = r;
        }
        else
            throw(std::runtime_error("crxp_istream: id mismatch"));
        return *this;
    }

    crxp_istream& operator>>(rxp_packet& p);
};

class crxp_ostream
{
    struct impl;
    impl* pimpl;
    
    rxp_ostream os;
    
    enum pack_state { key, diff, frame };
 
    void stop();
    void stop_rad();
    void stop_hr();
    
    void close_echo();
    void close_echo_hr();
 
    void compress_shot(const laser_shot_2angles<>& s);
    void compress_shot_rad(const laser_shot_2angles_rad<>& s_rad);
    void compress_shot_hr(const laser_shot_2angles_hr<>& s_hr);
    void compress_echo(const echo<>& p);
    void compress_echo_hr(const echo<>& p);
    void compress_echo_call(const echo<>& p);

public:

    crxp_ostream(const char* uri);
    crxp_ostream(const std::string& uri);
    ~crxp_ostream();
    
    operator void*();
    bool good() const;

    crxp_ostream& operator<<(package_id& x);
    crxp_ostream& operator<<(rxp_packet& p);
    
    template<class other_it>
    crxp_ostream& operator<<(const laser_shot_2angles<other_it>& s)
    {
        compress_shot(s);
        return *this;
    }
    template<class other_it>
    crxp_ostream& operator<<(const laser_shot_2angles_rad<other_it>& s_rad)
    {
        compress_shot_rad(s_rad);
        return *this;
    }
    template<class other_it>
    crxp_ostream& operator<<(const laser_shot_2angles_hr<other_it>& s_hr)
    {
        compress_shot_hr(s_hr);
        return *this;
    }
    template<class other_it>
    crxp_ostream& operator<<(const echo<other_it>& e)
    {
        compress_echo_call(e);
        return *this;
    }
    template<class other_it>
    crxp_ostream& operator<<(const header<other_it>& h)
    {
        os << h;
        return *this;
    }
};

} // namespace scanlib

#endif // CRXPSTREAM_HPP
