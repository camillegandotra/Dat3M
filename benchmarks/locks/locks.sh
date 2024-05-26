#!/bin/bash

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
WHITE='\033[1;37m'
NC='\033[0m' # No Color

# Define the available locks, tests, and relaxed options
locks=("bakery" "cas" "dekkers" "exchange" "filter" "petersons")
tests=("ls_litmus" "sl_litmus" "ll_litmus")
valid_relaxed_options=("acq2rx" "rel2rx")
default_memory_model="rc11"
available_models=("rc11" "tso" "aarch64")

# Path to locks.c (assuming it's in the same directory)
file_path="./locks.c"

# Path to lock dir and dat dir
lock_dir=$(pwd)
dat_dir="$(dirname "$0")/../../"

# Help msg
help() {
    echo -e "----------------------------------------------------"
    echo -e "${WHITE}locks.sh: Usage: $0 --config=<config_file>${NC}"
    echo -e "----------------------------------------------------"
    echo -e "Available options:"
    echo -e "    --config=<config_file>  Path to the JSON configuration file"
    echo -e "    --help                  Display this help message"
    echo -e "----------------------------------------------------"
    exit 0
}

# Log fnc for msgs
log() {
    local level=$1
    shift
    local msg="$@"
    local color
    case $level in
        INFO)
            color="${GREEN}"
            ;;
        DEBUG)
            color="${BLUE}"
            ;;
        ERROR)
            color="${RED}"
            ;;
        *)
            color="${NC}"
            ;;
    esac
    echo -e "${WHITE}locks.sh: ${color}$msg${NC}"
}

# Function to comment out all lock defines
comment_out_all_locks() {
    log DEBUG "Commenting out all locks..."
    sed -i '' -E 's/^(#define (BAKERY|CAS|DEKKERS|EXCHANGE|FILTER|PETERSONS))/\/\/ \1/' "$file_path"
}

# Function to uncomment a specific lock define
uncomment_lock() {
    local lock=$1
    log DEBUG "Uncommenting lock: $lock"
    sed -i '' "s/^\/\/ #define $lock/#define $lock/" "$file_path"
}

# Function to comment out all test defines
comment_out_all_tests() {
    log DEBUG "Commenting out all tests..."
    sed -i '' -E 's/^(#define (LS_LITMUS|SL_LITMUS|LL_LITMUS))/\/\/ \1/' "$file_path"
}

# Function to uncomment a specific test define
uncomment_test() {
    local test=$1
    log DEBUG "Uncommenting test: $test"
    sed -i '' "s/^\/\/ #define $test/#define $test/" "$file_path"
}

# Function to comment out all relaxed options
comment_out_all_relaxed_options() {
    log DEBUG "Commenting out all relaxed options..."
    sed -i '' -E 's/^(#define (ACQ2RX|REL2RX))/\/\/ \1/' "$file_path"
}

# Function to uncomment specific relaxed options
uncomment_relaxed_option() {
    local option=$1
    log DEBUG "Uncommenting relaxed option: $option"
    sed -i '' "s/^\/\/ #define $option/#define $option/" "$file_path"
}

# Convert array elements to lowercase
to_lowercase_array() {
    local arr=("$@")
    local lower_arr=()
    for item in "${arr[@]}"; do
        lower_arr+=("$(echo "$item" | tr '[:upper:]' '[:lower:]')")
    done
    echo "${lower_arr[@]}"
}

# Process each set of configs
process_config() {
    local lock=$(echo "$1" | tr '[:upper:]' '[:lower:]')
    local test=$(echo "$2" | tr '[:upper:]' '[:lower:]')
    local model=$(echo "$3" | tr '[:upper:]' '[:lower:]')
    shift 3
    local relaxed_options=($(to_lowercase_array "$@"))

    # Valid Lock?
    if [[ ! " ${locks[*]} " =~ " ${lock} " ]]; then
        log ERROR "Invalid lock: $lock"
        echo -e "${WHITE}locks.sh: ${NC}Available locks: ${locks[*]}"
        exit 1
    fi

    # Valid Test?
    if [[ ! " ${tests[*]} " =~ " ${test} " ]]; then
        log ERROR "Invalid test: $test"
        echo -e "${WHITE}locks.sh: ${NC}Available tests: ${tests[*]}"
        exit 1
    fi

    # Default memory model if not given
    if [ -z "$model" ]; then
        model=$default_memory_model
    fi

    # Target from model
    if [ "$model" == "rc11" ]; then
        target="c11"
    elif [ "$model" == "tso" ]; then
        target="tso"
    elif [ "$model" == "aarch64" ]; then
        target="arm8"
    else
        log ERROR "Invalid memory model: $model"
        echo -e "${WHITE}locks.sh: ${NC}Available memory models: ${available_models[*]}"
        exit 1
    fi

    # Edit locks.c
    comment_out_all_locks
    uncomment_lock "$(echo "$lock" | tr '[:lower:]' '[:upper:]')"

    comment_out_all_tests
    uncomment_test "$(echo "$test" | tr '[:lower:]' '[:upper:]')"

    comment_out_all_relaxed_options
    for option in "${relaxed_options[@]}"; do
        uncomment_relaxed_option "$(echo "$option" | tr '[:lower:]' '[:upper:]')"
    done

    # Execute dat
    log INFO "Configuration -> Lock: $lock, Test: $test, Relaxed Options: ${relaxed_options[*]}, Memory Model: $model, Target: $target"

    cd ${dat_dir}

    # Set environment variables
    export DAT3M_HOME=$(pwd)
    export DAT3M_OUTPUT=$DAT3M_HOME/output
    export CFLAGS="-I$DAT3M_HOME/include"
    export OPTFLAGS="-mem2reg -sroa -early-cse -indvars -loop-unroll -fix-irreducible -loop-simplify -simplifycfg -gvn"

    # Run the Java command (bound set to 10)
    java -jar dartagnan/target/dartagnan.jar cat/$model.cat --target=$target benchmarks/locks/locks.c --witness.graphviz=true --bound=5

    cd ${lock_dir}
}

# Main script logic
config_file=""

# Parse command line arguments
for arg in "$@"; do
    case $arg in
        --config=*)
            config_file="${arg#*=}"
            ;;
        --help)
            help
            ;;
        *)
            log ERROR "Invalid argument: $arg"
            help
            ;;
    esac
done

# Valid config file?
if [ -z "$config_file" ]; then
    log ERROR "Configuration file is required"
    help
fi

if [ ! -f "$config_file" ]; then
    log ERROR "Configuration file not found: $config_file"
    exit 1
fi

# Read and process the configurations from the JSON file (need jq)
configurations=$(jq -c '.configurations[]' "$config_file")

# Process each set of configs
for config in $configurations; do
    lock=$(echo $config | jq -r '.lock // ""')
    test=$(echo $config | jq -r '.test // ""')
    model=$(echo $config | jq -r '.model // ""')
    relaxed_options=($(echo $config | jq -r '.relaxed_options[] // ""'))

    # Set model to default if it is empty
    if [ -z "$model" ]; then
        model=$default_memory_model
    fi

    process_config $lock $test $model "${relaxed_options[@]}"
done
