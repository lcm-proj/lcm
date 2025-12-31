use rust_lcm_codegen::generate;
use std::env;
use std::fs;
use std::path::Path;

fn main() {
    // Rebuild when any LCM file changes under the crate-local directory
    let lcm_dir = Path::new("../types");
    println!("cargo:rerun-if-changed={}", lcm_dir.display());

    let schema_files: Vec<_> = fs::read_dir(lcm_dir)
        .expect("Failed to read lcm_files directory")
        .filter_map(|entry| {
            let entry = entry.ok()?;
            let path = entry.path();
            if path.extension().map_or(false, |ext| ext == "lcm") {
                Some(path)
            } else {
                None
            }
        })
        .collect();

    let out_dir = env::var("OUT_DIR").expect("OUT_DIR");
    let out_path = Path::new(&out_dir).join("generated_lcm_api.rs");
    generate(schema_files, &out_path);
}
