// $Id: scanmech.hpp 1125 2014-11-17 08:08:01Z RS $

#ifndef DEVGEOMETRY_HPP
#define DEVGEOMETRY_HPP

#include <riegl/config.hpp>

#include <cstddef>
#include <vector>

namespace scanlib {

class scanmech {
public:

    enum kind_e { 
        unknown
        , mirrorwheel
        , wedge 
        , mirrorwheel_exitpane
        , mirrorwheel_deflection
        , mirrorwheel_biaxial
        , mirrorwheel_exitpane_deflection
    };
    
    typedef double vec3d[3];

    // computed results
    vec3d& beam_origin;
    vec3d& beam_direction;

    double line_unit;
    double frame_unit;

    unsigned facet;
    double line_angle;
    double frame_angle;
    double sin_line, cos_line;
    double sin_frame, cos_frame;

    // input parameters
    double range_unit;
    uint32_t line_circle_count;
    uint32_t frame_circle_count;
    
    scanmech(
        vec3d& beam_origin
        , vec3d& beam_direction
    );

    virtual ~scanmech();
    
    virtual kind_e kind() const { return scanmech::unknown; }

    void 
    precompute();

    virtual void
    compute_beam(
        unsigned facet
        , double line_angle
        , double frame_angle
    );
    
    virtual void 
    compute_beam_raw(
        uint32_t line_angle_raw
        , uint32_t frame_angle_raw = 0
        , unsigned segment = 0
    );
    
    inline double
    compute_range(
        int32_t range_raw
    ) {
        return range_unit*range_raw;
    }
    
    virtual void compute_vertex(
        vec3d& vertex
        ,double range
    ) {
        for (std::size_t n=0; n<3; ++n)
            vertex[n] = beam_origin[n] + range*beam_direction[n];
    }
    
};
/*------------------------------------------------------------------*/
class mirrorwheel
    : public scanmech {

protected:
    // computed results
    int32_t line_modulus;

    vec3d f;
    struct precomp_facet_t {
        vec3d a;
        vec3d b;
        vec3d c;
        double d;
    };
    std::vector<precomp_facet_t> pcf;

    vec3d direction, origin, normal;
    
public:
    struct facet_t {
        vec3d normal;
        double dist;
    };

    // input parameters
    vec3d laser_origin;
    vec3d laser_direction;
    vec3d mirror_axis_origin;
    vec3d mirror_axis_direction;
    int32_t line_angle_0;
    std::vector<facet_t> facets;
    
    mirrorwheel(
        vec3d& beam_origin
        , vec3d& beam_direction
    );
    
    mirrorwheel(
        const scanmech& sm
    );

    virtual kind_e kind() const { return scanmech::mirrorwheel; }
    
    void 
    precompute();
    
    virtual void
    compute_beam(
        unsigned facet
        , double line_angle
        , double frame_angle
    );

    virtual void 
    compute_beam_raw(
        uint32_t line_angle_raw
        , uint32_t frame_angle_raw = 0
        , unsigned segment = 0
    );
};
/*------------------------------------------------------------------*/
class mirrorwheel_exitpane
    : public mirrorwheel {

protected:
    // computed results
    double n2;
    vec3d o;
    vec3d M[3];
        
public:
    // input parameters
    double exit_pane_thickness;
    double exit_pane_index;
    vec3d exit_pane_direction;
    
    mirrorwheel_exitpane(
        vec3d& beam_origin
        , vec3d& beam_direction
    );
    
    mirrorwheel_exitpane(
        const scanlib::mirrorwheel& mw
    );
    
    virtual kind_e kind() const { return scanmech::mirrorwheel_exitpane; }
    
    void 
    precompute();
    
    virtual void 
    compute_beam(
        unsigned facet
        , double line_angle
        , double frame_angle
    );
};
/*------------------------------------------------------------------*/
class mirrorwheel_deflection
    : public mirrorwheel {
        
protected:
    // computed results
    double t0;
    double tmod;
    vec3d cd;
    
public:
    // input parameters
    vec3d deflection_mirror_origin;
    vec3d deflection_mirror_direction;
    
    mirrorwheel_deflection(
        vec3d& beam_origin
        , vec3d& beam_direction
    );
    
    mirrorwheel_deflection(
        const scanlib::mirrorwheel& mw
    );
    
    virtual kind_e kind() const { return scanmech::mirrorwheel_deflection; }
    
    void 
    precompute();
    
    virtual void
    compute_beam(
        unsigned facet
        , double line_angle
        , double frame_angle
    );

    virtual void 
    compute_beam_raw(
        uint32_t line_angle_raw
        , uint32_t frame_angle_raw = 0
        , unsigned segment = 0
    );
};
/*------------------------------------------------------------------*/
class mirrorwheel_biaxial
    : public mirrorwheel {

protected:
    // computed results
    vec3d biaxial_direction;
    double range_max;
    double range_step;
    std::vector<double> shift;
    
public:
    // input parameters
    vec3d aperture_direction;
    std::vector<std::pair<float, float> > biaxial_shift; 
    
    mirrorwheel_biaxial(
        vec3d& beam_origin
        , vec3d& beam_direction
    );
    
    mirrorwheel_biaxial(
        const scanlib::mirrorwheel& mw
    );
    
    virtual kind_e kind() const { return scanmech::mirrorwheel_biaxial; }
    
    void 
    precompute();
    
    virtual void
    compute_beam(
        unsigned facet
        , double line_angle
        , double frame_angle
    );
    
    virtual void compute_vertex(
        vec3d& vertex
        ,double range
    );
};
/*------------------------------------------------------------------*/
class wedge
    : public scanmech {

protected:
    // computed results
    vec3d a1, b1, c1;
    vec3d a2, b2, c2;
    double mu;
    vec3d wolo, wolowd;
    
public:
    // input parameters
    vec3d laser_origin;
    vec3d laser_direction;
    vec3d wedge_axis_origin;
    vec3d wedge_axis_direction;
    vec3d facet1_direction;
    vec3d facet2_direction;
    double facet_distance;
    double refraction_index;
    
    wedge(
        vec3d& beam_origin
        , vec3d& beam_direction
    );

    wedge(
        const scanmech& sm
    );
    
    virtual kind_e kind() const { return scanmech::wedge; }
    
    void 
    precompute();
    
    virtual void
    compute_beam(
        unsigned facet
        , double line_angle
        , double frame_angle
    );

    virtual void 
    compute_beam_raw(
        uint32_t line_angle_raw
        , uint32_t frame_angle_raw = 0
        , unsigned segment = 0
    );
    
};
    
} // namespace scanlib

#endif // DEVGEOMETRY_HPP
