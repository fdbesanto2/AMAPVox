// $Id: wave.hpp 1055 2014-06-26 06:52:52Z RS $
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

    wave(bool sync_to_pps = false, bool decompress = false);
    ~wave();

    struct channel_t {
        double sample_rate;
        double wavelength;
        double delta_st;
        std::string name;
    };
    
    std::vector<channel_t> channel_properties;
    std::size_t reference_channel_index;
    
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
        std::size_t channel;
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
        struct statistic_t { float mean; float stdev; };
        std::vector<statistic_t> channel_statistic;
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
    void on_units_3(
        const units_3<iterator_type>& arg
    );
    void on_pps_sync(
        const pps_sync<iterator_type>& arg
    );
    void on_pps_sync_hr(
        const pps_sync_hr<iterator_type>& arg
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
    void on_sbl_dg_channel(
        const sbl_dg_channel<iterator_type>& arg
    );
    void on_sbl_dg_channel_header(
        const sbl_dg_channel_header<iterator_type>& arg
    );
    void on_sbl_dg_channel_data(
        const sbl_dg_channel_data<iterator_type>& arg
    );
    void on_sbl_dg_channel_data_compressed(
        const sbl_dg_channel_data_compressed<iterator_type>& arg
    );
    void on_shot_end(
    );
    void on_calib_table(
        const calib_table<iterator_type>& arg
    );

private:
    std::pair<double,double> times;
    double systime_hr_unit;
    unsigned systime_hr_bits;
    double systime_unit;

    std::vector<uint16_t> data_compressed;
    detail::clock_compensator wave_clock;
    std::deque<waveform*> fifo;
    bool have_wave;
    bool decompress;
    bool sbl_dg_parameters_seen; // can possibly removed
    
    void fill_lookup_table(lookup_table& table, const calib_table<iterator_type>& arg);
    
};

} // end namespace wavelib

#endif // WAVE_HPP
