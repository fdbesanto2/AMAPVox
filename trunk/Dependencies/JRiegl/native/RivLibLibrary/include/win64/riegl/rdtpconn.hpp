// $Id: rdtpconn.hpp 560 2011-03-28 13:58:08Z rs $

//!\file rdtpconn.hpp
//! The R-iegl D-ata T-transfer P-rotocol classes
#ifndef RDTPCONN_HPP
#define RDTPCONN_HPP

#include <riegl/connection.hpp>

#include <string>

namespace scanlib {
//*****************************************************************************
//  rdtp_rconnection
//*****************************************************************************
class RIVLIB_API rdtp_rconnection
    : public basic_rconnection
{
public:
    //! constructor for rdtp read connection
    //!\param file_uri a rdtp specifier e.g. rdtp://192.168.0.42/CURRENT
    //!\param continuation a continuation string from a previous connnection
    rdtp_rconnection(
        const std::string& rdtp_uri
        , const std::string& continuation = std::string()
    );

    ~rdtp_rconnection();

    //! open the connection,
    //! override of base open
    void open();
    //! close the connection,
    //! override of base close
    void close();
    //! cancel request,
    //! override of base cancel
    void cancel();
    //! request for shutdown of the connection,
    //! override of base
    void request_shutdown();

protected:

    size_type more_input(
        void* buf
        , size_type count
    );

    //! The continuation string
    //!\return the continuation string for use in a later resume attempt
    std::string continuation() const;

private:
    class impl;
    impl* pimpl;
    rdtp_rconnection(const rdtp_rconnection&);
    rdtp_rconnection operator=(const rdtp_rconnection&);
};

} // namespace scanlib


#endif // RDTPCONN_HPP
