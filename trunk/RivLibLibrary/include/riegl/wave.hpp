// $Id: wave.hpp 719 2012-10-30 10:36:32Z RS $
#ifndef WAVE_HPP
#define WAVE_HPP

#include <riegl/pointcloud.hpp>
#include <riegl/detail/clockcomp.hpp>

#include <vector>
#include <deque>

namespace scanlib {

class wave
    : public scanlib::pointcloud
{
public:
    typedef enum {
        low_power           = 1
        , high_power        = 0
        , super_high_power  = 2
        , ref_pulse         = 3
    } channel_type;

    wave(bool sync_to_pps = false);
    ~wave();

    double group_velocity;
    double sampling_time;
    double delta_st;

    struct lookup_table {
        double abscissa_scale;
        double abscissa_offset;
        double ordinate_scale;
        double ordinate_offset;
        std::vector<int16_t> abscissa;
        std::vector<int16_t> ordinate;
        double operator()(double x);
    };

    lookup_table amplitude_low_power;
    lookup_table amplitude_high_power;
    lookup_table position_diff_low_power;
    lookup_table position_diff_high_power;

protected:
    struct wavelet{
        wavelet() : time(0), channel(), compressed(false) {}
        double time;
        channel_type channel;
        bool compressed;
        std::vector<uint16_t> data;
    };
    struct waveform {
        double time_sorg;
        double time;
        double origin[3];
        double direction[3];
        unsigned facet;
        double line_angle;
        double frame_angle;
        bool pps;
        std::vector<wavelet> data;
    };
    virtual void on_wave(
        waveform& wfm
    ) = 0;

    void on_units(
        const units<iterator_type>& arg
    );
    void on_units_2(
        const units_2<iterator_type>& arg
    );
    void on_pps_sync(
        const pps_sync<iterator_type>& arg
    );
    void on_sbl_dg_parameters(
        const sbl_dg_parameters<iterator_type>& arg
    );
    void on_sbl_dg_header(
        const sbl_dg_header<iterator_type>& arg
    );
    void on_sbl_dg_data(
        const sbl_dg_data<iterator_type>& arg
    );
    void on_sbl_dg_data_compressed(
        const sbl_dg_data_compressed<iterator_type>& arg
    );
    void on_sbl_dg_header_hr(
        const sbl_dg_header_hr<iterator_type>& arg
    );
    void on_sbl_dg_data_hr(
        const sbl_dg_data_hr<iterator_type>& arg
    );
    void on_sbl_dg_data_compressed_hr(
        const sbl_dg_data_compressed_hr<iterator_type>& arg
    );
    void on_shot_end(
    );
    void on_calib_table(
        const calib_table<iterator_type>& arg
    );
private:
    std::pair<double,double> times;
    double systime_hr_unit;
    double systime_unit;
    uint64_t systime_hr_sorg;
    double sorg_hr_offset;

    std::vector<uint16_t> data_compressed;
    detail::clock_compensator wave_clock;
    std::deque<waveform*> fifo;

    void fill_lookup_table(lookup_table& table, const calib_table<iterator_type>& arg);
};

} // end namespace wavelib

#endif // WAVE_HPP
