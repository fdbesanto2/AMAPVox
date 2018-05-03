// $Id: clockcomp.hpp 677 2012-05-24 09:12:38Z RS $

#ifndef CLOCKCOMP_HPP
#define CLOCKCOMP_HPP

#include <riegl/config.hpp>

#include <utility>

namespace scanlib { namespace detail {

class clock_compensator
{
    double sys_unit;
    double pps_unit;
    double sys_unit_hr;

    uint32_t sync_pps_prev;
    uint64_t sync_pps_ext;
    uint32_t sync_sys_prev;
    uint32_t sync_sys_ext;
    double drift;
    double skew;

    uint32_t sys_prev;
    uint32_t sys_ext;

    bool pre_locked;
    bool locked;
    bool once_locked;

    bool unwrap_once;

public:
    clock_compensator();

    void
    init(
        double sys_unit_
        , double pps_unit_
    );

    void
    init(
        double sys_unit_
        , double pps_unit_
        , double sys_unit_hr_
    );

    std::pair<double,double>
    sync(
        uint32_t sys_
        , uint32_t pps_
    );

    std::pair<double,double>
    unwrap(
        uint32_t sys_
    );

    std::pair<double,double>
    unwrap_hr(
        uint64_t sys_hr_
    );

    bool
    is_locked(
    ) const {
        return locked;
    }

    bool
    is_once_locked(
    ) const {
        return once_locked;
    }
};

}} // namespace detail namespace scanlib

#endif // CLOCKCOMP_HPP

