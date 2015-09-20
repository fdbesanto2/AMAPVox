// $Id: compressed.hpp 555 2011-03-03 13:05:58Z rs $

#ifndef COMPRESSED_HPP
#define COMPRESSED_HPP

#include <riegl/config.hpp>
#include <riegl/connection.hpp>
#include <riegl/buffer.hpp>
#include <riegl/rmsmarker.hpp>
#include <riegl/ridataspec.hpp>

#include <deque>
#include <utility>

namespace scanlib {

class RIVLIB_API compressed_packets
    :  virtual public basic_packets
{
public:
    compressed_packets();
    virtual ~compressed_packets();

protected:
    void on_packed_key_laser_shot_2angles(const packed_key_laser_shot_2angles<iterator_type>& p);
    void on_packed_key_laser_shot_2angles_rad(const packed_key_laser_shot_2angles_rad<iterator_type>& p);
    void on_packed_key_laser_shot_2angles_hr(const packed_key_laser_shot_2angles_hr<iterator_type>& p);
    void on_packed_frame_laser_shot_2angles(const packed_frame_laser_shot_2angles<iterator_type>& p);
    void on_packed_frame_laser_shot_2angles_rad(const packed_frame_laser_shot_2angles_rad<iterator_type>& p);
    void on_packed_frame_laser_shot_2angles_hr(const packed_frame_laser_shot_2angles_hr<iterator_type>& p);
    void on_packed_key_echo(const packed_key_echo<iterator_type>& p);
    void on_packed_key_echo_hr(const packed_key_echo_hr<iterator_type>& p);
    void on_packed_frame_echo(const packed_frame_echo<iterator_type>& p);
    void on_packed_frame_echo_hr(const packed_frame_echo_hr<iterator_type>& p);

    private:
    enum mode_t { uninitialized, angle_only, rad, hr } mode;
    unsigned pending_echoes;
    std::deque<std::pair<laser_shot_2angles<>, unsigned> > qs;
    std::deque<std::pair<laser_shot_2angles_rad<>, unsigned> > qs_rad;
    std::deque<std::pair<laser_shot_2angles_hr<>, unsigned> > qs_hr;
    std::deque<echo<> > qe;
    laser_shot_2angles<> sp;
    laser_shot_2angles_rad<> sp_rad;
    laser_shot_2angles_hr<> sp_hr;
    echo<> ep;
    void check(mode_t mode_);
    void drain();
    void drain_rad();
    void drain_hr();
    void resync();
    bool echo_seen;
};

}

#endif // COMPRESSED_HPP
