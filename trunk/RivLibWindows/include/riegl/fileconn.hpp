// $Id: fileconn.hpp 1118 2014-11-05 11:40:29Z RS $

//!\file fileconn.hpp
//! The file connection classes

#ifndef FILECONN_HPP
#define FILECONN_HPP

#include <riegl/connection.hpp>

#include <cstdio>
#include <string>
#include <csignal>
#include <vector>
#include <fstream>

namespace scanlib {

//!\brief the file connection class
class RIVLIB_API file_rconnection
    : public basic_rconnection
{
public:
    //! constructor for file connection
    //!\param file_uri a file specifier e.g. file:filename.rxp
    //!\param continuation not applicable to file connections
    file_rconnection(
        const std::string& file_uri
        , const std::string& continuation = std::string()
    );

    ~file_rconnection();

    //! Seek to a possition that has been retrieved with tellg
    file_rconnection&
    seekg(
        pos_type pos
    );

    //! The cancel request
    //! note: this function is not really useful for files
    void cancel();
    //! The shutdown request
    //! note: this function is not really useful for files
    void request_shutdown();

protected:
    virtual size_type more_input(
        void* buf
        , size_type count
    );

private:
    std::string filename;
    std::filebuf pre_file;
    std::filebuf file;
    std::vector<std::fstream::char_type> buffer;
    std::sig_atomic_t is_cancelled;
};

//!\brief INTERNAL ONLY
class RIVLIB_API file_wconnection
    : public basic_wconnection
{
public:
    file_wconnection(
        const std::string& file_uri
    );

    ~file_wconnection();

    virtual size_type write(
        const void* buf
        , size_type count
    );

private:
    FILE* file;
};

} // namespace scanlib

#endif // FILECONN_HPP
